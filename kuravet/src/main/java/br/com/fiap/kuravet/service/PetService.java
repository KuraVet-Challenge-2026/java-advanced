package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.domain.entity.Pet;
import br.com.fiap.kuravet.domain.entity.Tutor;
import br.com.fiap.kuravet.domain.repository.PetRepository;
import br.com.fiap.kuravet.domain.repository.TutorRepository;
import br.com.fiap.kuravet.dto.PetRequestDTO;
import br.com.fiap.kuravet.dto.PetResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TutorRepository tutorRepository;

    public PetResponseDTO salvar(PetRequestDTO dto) {
        Tutor tutor = tutorRepository.findById(dto.idTutor())
                .orElseThrow(() -> new RuntimeException("Tutor nao encontrado"));

        Pet pet = new Pet();
        pet.setTutor(tutor);
        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());

        Pet petSalvo = petRepository.save(pet);
        return converterParaDTO(petSalvo);
    }

    // Traz todos os pets paginados e guarda em cache
    @Cacheable("todosPets")
    public Page<PetResponseDTO> listarTodos(Pageable pageable) {
        return petRepository.findAll(pageable)
                .map(this::converterParaDTO);
    }

    // Busca por espécie com paginação e guarda em cache
    @Cacheable("petsPorEspecie")
    public Page<PetResponseDTO> buscarPorEspecie(String especie, Pageable pageable) {
        return petRepository.findByEspecieIgnoreCase(especie, pageable)
                .map(this::converterParaDTO);
    }

    private PetResponseDTO converterParaDTO(Pet pet) {
        return new PetResponseDTO(
                pet.getId(),
                pet.getTutor().getId(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getDataNascimento(),
                pet.getScoreVitalidade()
        );
    }
}