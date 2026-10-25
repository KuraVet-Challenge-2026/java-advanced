package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.triagem.TriagemRequestDTO;
import br.com.fiap.kuravet.exception.TriagemNaoEncontradaException;
import br.com.fiap.kuravet.ia.ContextoOrientacao;
import br.com.fiap.kuravet.ia.ExtratorComFallback;
import br.com.fiap.kuravet.ia.GeradorOrientacaoComFallback;
import br.com.fiap.kuravet.ia.MotorRegrasTriagem;
import br.com.fiap.kuravet.ia.OrientacaoGerada;
import br.com.fiap.kuravet.ia.PerfilPet;
import br.com.fiap.kuravet.ia.ResultadoExtracao;
import br.com.fiap.kuravet.ia.ResultadoTriagem;
import br.com.fiap.kuravet.ia.SintomasExtraidos;
import br.com.fiap.kuravet.ia.Urgencia;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Triagem;
import br.com.fiap.kuravet.repository.TriagemRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

@Service
public class TriagemService {

    private static final Logger log = LoggerFactory.getLogger(TriagemService.class);

    // Mais urgentes primeiro (ordem do enum Urgencia) e, no mesmo nivel, as mais recentes.
    static final Comparator<Triagem> MAIS_URGENTES_PRIMEIRO = Comparator
            .comparing(Triagem::getUrgencia).reversed()
            .thenComparing(Triagem::getDataTriagem, Comparator.reverseOrder());

    private final TriagemRepository triagemRepository;
    private final PetService petService;
    private final ExtratorComFallback extrator;
    private final MotorRegrasTriagem motorRegrasTriagem;
    private final GeradorOrientacaoComFallback geradorOrientacao;

    public TriagemService(TriagemRepository triagemRepository,
                          PetService petService,
                          ExtratorComFallback extrator,
                          MotorRegrasTriagem motorRegrasTriagem,
                          GeradorOrientacaoComFallback geradorOrientacao) {
        this.triagemRepository = triagemRepository;
        this.petService = petService;
        this.extrator = extrator;
        this.motorRegrasTriagem = motorRegrasTriagem;
        this.geradorOrientacao = geradorOrientacao;
    }

    @Transactional
    public Triagem realizar(UsuarioPrincipal principal, TriagemRequestDTO dto) {
        Pet pet = petService.buscarPorId(principal, dto.idPet());

        PerfilPet perfil = new PerfilPet(pet.getEspecie(), idadeEmAnos(pet.getDataNascimento()));
        ResultadoExtracao extracao = extrator.extrair(dto.relato());
        SintomasExtraidos sintomas = extracao.sintomas();
        ResultadoTriagem resultado = motorRegrasTriagem.avaliar(sintomas, perfil);
        OrientacaoGerada orientacao = gerarOrientacao(pet, perfil, dto.relato(), sintomas, resultado);

        Triagem triagem = Triagem.builder()
                .pet(pet)
                .relato(dto.relato())
                .sintomas(sintomas.sintomas())
                .urgencia(resultado.urgencia())
                .recomendacao(resultado.recomendacao())
                .regrasAplicadas(resultado.regrasAplicadas())
                .metodoExtracao(extracao.metodo())
                .dataTriagem(LocalDateTime.now())
                .orientacao(orientacao == null ? null : orientacao.texto())
                .origemOrientacao(orientacao == null ? null : orientacao.origem())
                .build();

        Triagem salva = triagemRepository.save(triagem);

        // O relato e dado do tutor e nao vai para o log.
        log.info("Triagem registrada: idPet={}, urgencia={}, metodo={}, orientacao={}",
                pet.getIdPet(), salva.getUrgencia(), salva.getMetodoExtracao(), salva.getOrigemOrientacao());

        return salva;
    }

    // Roda depois do motor e so recebe o resultado pronto: nao altera urgencia nem recomendacao.
    // Qualquer falha aqui deixa a triagem sem orientacao, mas nunca impede que ela seja salva.
    private OrientacaoGerada gerarOrientacao(Pet pet, PerfilPet perfil, String relato,
                                             SintomasExtraidos sintomas, ResultadoTriagem resultado) {
        try {
            List<ContextoOrientacao.TriagemAnterior> anteriores = triagemRepository
                    .findTop3ByPetIdPetOrderByDataTriagemDesc(pet.getIdPet()).stream()
                    .map(t -> new ContextoOrientacao.TriagemAnterior(
                            t.getDataTriagem(), t.getUrgencia(), t.getSintomas()))
                    .toList();

            return geradorOrientacao.gerar(new ContextoOrientacao(
                    pet.getNome(), pet.getEspecie(), perfil.idadeAnos(),
                    resultado.urgencia(), resultado.recomendacao(), sintomas.sintomas(), relato, anteriores));
        } catch (RuntimeException e) {
            log.warn("Triagem salva sem orientação personalizada: {}", e.getClass().getSimpleName());
            return null;
        }
    }

    public List<Triagem> listar(UsuarioPrincipal principal) {
        List<Triagem> triagens = principal.isTutor()
                ? triagemRepository.findByPetTutorIdTutor(principal.getIdTutor())
                : triagemRepository.findAll();

        return triagens.stream().sorted(MAIS_URGENTES_PRIMEIRO).toList();
    }

    public List<Triagem> listarPorUrgencia(UsuarioPrincipal principal, Urgencia urgencia) {
        return listar(principal).stream()
                .filter(triagem -> triagem.getUrgencia() == urgencia)
                .toList();
    }

    // ALTA e EMERGENCIA: as que pedem teleconsulta prioritaria ou atendimento imediato.
    public List<Triagem> listarUrgentes(UsuarioPrincipal principal) {
        return listar(principal).stream()
                .filter(triagem -> triagem.getUrgencia().sugereTeleconsultaPrioritaria())
                .toList();
    }

    // O ownership vem do PetService: pet de outro tutor responde como inexistente.
    public List<Triagem> listarPorPet(UsuarioPrincipal principal, Long idPet) {
        Pet pet = petService.buscarPorId(principal, idPet);

        return triagemRepository.findByPetIdPet(pet.getIdPet()).stream()
                .sorted(Comparator.comparing(Triagem::getDataTriagem, Comparator.reverseOrder()))
                .toList();
    }

    // Soma apenas ALTA e EMERGENCIA; MODERADA e BAIXA nao entram na conta.
    public long contarUrgentesDesde(LocalDateTime desde) {
        return triagemRepository.countByUrgenciaAndDataTriagemGreaterThanEqual(Urgencia.ALTA, desde)
                + triagemRepository.countByUrgenciaAndDataTriagemGreaterThanEqual(Urgencia.EMERGENCIA, desde);
    }

    public Triagem buscarPorId(UsuarioPrincipal principal, Long id) {
        Triagem triagem = triagemRepository.findById(id)
                .orElseThrow(() -> new TriagemNaoEncontradaException(id));

        if (principal.isTutor() && !triagem.getPet().getTutor().getIdTutor().equals(principal.getIdTutor())) {
            throw new TriagemNaoEncontradaException(id);
        }

        return triagem;
    }

    private static int idadeEmAnos(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return 0;
        }
        return Math.max(0, Period.between(dataNascimento, LocalDate.now()).getYears());
    }
}
