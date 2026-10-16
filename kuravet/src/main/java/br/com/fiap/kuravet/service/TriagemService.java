package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.triagem.TriagemRequestDTO;
import br.com.fiap.kuravet.exception.TriagemNaoEncontradaException;
import br.com.fiap.kuravet.ia.ExtratorSintomas;
import br.com.fiap.kuravet.ia.MotorRegrasTriagem;
import br.com.fiap.kuravet.ia.PerfilPet;
import br.com.fiap.kuravet.ia.ResultadoTriagem;
import br.com.fiap.kuravet.ia.SintomasExtraidos;
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
    private final ExtratorSintomas extratorSintomas;
    private final MotorRegrasTriagem motorRegrasTriagem;

    public TriagemService(TriagemRepository triagemRepository,
                          PetService petService,
                          ExtratorSintomas extratorSintomas,
                          MotorRegrasTriagem motorRegrasTriagem) {
        this.triagemRepository = triagemRepository;
        this.petService = petService;
        this.extratorSintomas = extratorSintomas;
        this.motorRegrasTriagem = motorRegrasTriagem;
    }

    @Transactional
    public Triagem realizar(UsuarioPrincipal principal, TriagemRequestDTO dto) {
        Pet pet = petService.buscarPorId(principal, dto.idPet());

        PerfilPet perfil = new PerfilPet(pet.getEspecie(), idadeEmAnos(pet.getDataNascimento()));
        SintomasExtraidos sintomas = extratorSintomas.extrair(dto.relato());
        ResultadoTriagem resultado = motorRegrasTriagem.avaliar(sintomas, perfil);

        Triagem triagem = Triagem.builder()
                .pet(pet)
                .relato(dto.relato())
                .sintomas(sintomas.sintomas())
                .urgencia(resultado.urgencia())
                .recomendacao(resultado.recomendacao())
                .regrasAplicadas(resultado.regrasAplicadas())
                .metodoExtracao(extratorSintomas.metodo())
                .dataTriagem(LocalDateTime.now())
                .build();

        Triagem salva = triagemRepository.save(triagem);

        // O relato e dado do tutor e nao vai para o log.
        log.info("Triagem registrada: idPet={}, urgencia={}, metodo={}",
                pet.getIdPet(), salva.getUrgencia(), salva.getMetodoExtracao());

        return salva;
    }

    public List<Triagem> listar(UsuarioPrincipal principal) {
        List<Triagem> triagens = principal.isTutor()
                ? triagemRepository.findByPetTutorIdTutor(principal.getIdTutor())
                : triagemRepository.findAll();

        return triagens.stream().sorted(MAIS_URGENTES_PRIMEIRO).toList();
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
