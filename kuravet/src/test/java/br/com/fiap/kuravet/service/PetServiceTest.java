package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.pet.PetRequestDTO;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.repository.ConsultaRepository;
import br.com.fiap.kuravet.repository.PetRepository;
import br.com.fiap.kuravet.repository.TutorRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    private PetService petService;

    private static final Long ID_TUTOR = 1L;

    @BeforeEach
    void setUp() {
        petService = new PetService(petRepository, tutorRepository, consultaRepository);
    }

    // ---------- helpers ----------

    private Tutor tutor(Long idTutor) {
        return Tutor.builder().idTutor(idTutor).nome("Tutor " + idTutor).cpf("111.111.111-1" + idTutor).build();
    }

    private UsuarioPrincipal principalTutor(Long idTutor) {
        Usuario usuario = Usuario.builder()
                .idUsuario(idTutor)
                .username("tutor" + idTutor)
                .senha("senha")
                .perfil(Perfil.TUTOR)
                .tutor(tutor(idTutor))
                .build();
        return new UsuarioPrincipal(usuario);
    }

    private Pet pet(Long idPet, Long idTutor, String codigoIdentificacao) {
        return Pet.builder()
                .idPet(idPet)
                .nome("Rex")
                .especie("Cachorro")
                .dataNascimento(LocalDate.of(2020, 1, 1))
                .sexo('M')
                .tutor(tutor(idTutor))
                .codigoIdentificacao(codigoIdentificacao)
                .build();
    }

    private PetRequestDTO dto(String codigoIdentificacao) {
        return new PetRequestDTO("Rex", "Cachorro", "Vira-lata",
                LocalDate.of(2020, 1, 1), 'M', codigoIdentificacao);
    }

    private void mockSalvarRetornandoArgumento() {
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ---------- normalizacao ----------

    @Test
    void deveNormalizarCodigoDeIdentificacaoParaMaiusculoSemEspacosNasPontas() {
        when(tutorRepository.findById(ID_TUTOR)).thenReturn(Optional.of(tutor(ID_TUTOR)));
        when(petRepository.existsByCodigoIdentificacao("PZ-123")).thenReturn(false);
        mockSalvarRetornandoArgumento();

        Pet resultado = petService.cadastrar(ID_TUTOR, dto(" pz-123 "));

        assertThat(resultado.getCodigoIdentificacao()).isEqualTo("PZ-123");
    }

    @Test
    void deveNormalizarCodigoDeIdentificacaoEmBrancoParaNulo() {
        when(tutorRepository.findById(ID_TUTOR)).thenReturn(Optional.of(tutor(ID_TUTOR)));
        mockSalvarRetornandoArgumento();

        Pet resultado = petService.cadastrar(ID_TUTOR, dto("   "));

        assertThat(resultado.getCodigoIdentificacao()).isNull();
    }

    // ---------- duplicidade ----------

    @Test
    void deveLancarRegraDeNegocioAoCadastrarComCodigoDeIdentificacaoDuplicado() {
        when(tutorRepository.findById(ID_TUTOR)).thenReturn(Optional.of(tutor(ID_TUTOR)));
        when(petRepository.existsByCodigoIdentificacao("PZ-123")).thenReturn(true);

        assertThrows(RegraDeNegocioException.class,
                () -> petService.cadastrar(ID_TUTOR, dto("pz-123")));

        verify(petRepository, never()).save(any());
    }

    @Test
    void naoDeveAcusarDuplicidadeAoAtualizarPetMantendoOProprioCodigo() {
        Pet petExistente = pet(5L, ID_TUTOR, "PZ-999");
        when(petRepository.findById(5L)).thenReturn(Optional.of(petExistente));
        when(petRepository.existsByCodigoIdentificacaoAndIdPetNot("PZ-999", 5L)).thenReturn(false);
        mockSalvarRetornandoArgumento();

        Pet resultado = petService.atualizar(principalTutor(ID_TUTOR), 5L, dto("pz-999"));

        assertThat(resultado.getCodigoIdentificacao()).isEqualTo("PZ-999");
        verify(petRepository).save(petExistente);
    }

    @Test
    void naoDeveConsultarDuplicidadeQuandoCodigoDeIdentificacaoNaoEInformado() {
        when(tutorRepository.findById(ID_TUTOR)).thenReturn(Optional.of(tutor(ID_TUTOR)));
        mockSalvarRetornandoArgumento();

        petService.cadastrar(ID_TUTOR, dto(null));

        verify(petRepository, never()).existsByCodigoIdentificacao(any());
        verify(petRepository, never()).existsByCodigoIdentificacaoAndIdPetNot(any(), anyLong());
    }
}
