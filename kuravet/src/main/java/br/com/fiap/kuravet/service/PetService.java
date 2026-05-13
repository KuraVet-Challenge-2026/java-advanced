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
import org.springframework.cache.annotation.CacheEvict;

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

    // Atualiza um Pet existente e limpa os caches antigos
    @CacheEvict(value = {"todosPets", "petsPorEspecie"}, allEntries = true)
    public PetResponseDTO atualizar(Long id, PetRequestDTO dto) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet não encontrado para atualização"));

        Tutor tutor = tutorRepository.findById(dto.idTutor())
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));

        pet.setTutor(tutor);
        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());

        Pet petAtualizado = petRepository.save(pet);
        return converterParaDTO(petAtualizado);
    }

    // Exclui um Pet e limpa os caches
    @CacheEvict(value = {"todosPets", "petsPorEspecie"}, allEntries = true)
    public void excluir(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet não encontrado para exclusão"));

        petRepository.delete(pet);
    }

}