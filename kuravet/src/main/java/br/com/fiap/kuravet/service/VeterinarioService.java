package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.exception.VeterinarioNaoEncontradoException;
import br.com.fiap.kuravet.model.Veterinario;
import br.com.fiap.kuravet.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;

    public VeterinarioService(VeterinarioRepository veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<Veterinario> listar() {
        return veterinarioRepository.findAll();
    }

    public Veterinario buscarPorId(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(id));
    }
}
