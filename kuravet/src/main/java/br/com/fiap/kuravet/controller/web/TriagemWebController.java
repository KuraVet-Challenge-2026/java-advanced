package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.ia.Urgencia;
import br.com.fiap.kuravet.model.Triagem;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.PetService;
import br.com.fiap.kuravet.service.TriagemService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// Somente visualizacao: a triagem e criada pela API (app do tutor) e a LLM nunca e chamada aqui.
@Controller
public class TriagemWebController {

    private final TriagemService triagemService;
    private final PetService petService;

    public TriagemWebController(TriagemService triagemService, PetService petService) {
        this.triagemService = triagemService;
        this.petService = petService;
    }

    @GetMapping("/portal/triagens")
    public String fila(@AuthenticationPrincipal UsuarioPrincipal principal,
                       @RequestParam(required = false) Urgencia urgencia,
                       @RequestParam(defaultValue = "false") boolean urgentes,
                       Model model) {

        List<Triagem> triagens;
        if (urgencia != null) {
            triagens = triagemService.listarPorUrgencia(principal, urgencia);
        } else if (urgentes) {
            triagens = triagemService.listarUrgentes(principal);
        } else {
            triagens = triagemService.listar(principal);
        }

        model.addAttribute("triagens", triagens);
        model.addAttribute("urgenciaSelecionada", urgencia);
        model.addAttribute("somenteUrgentes", urgencia == null && urgentes);
        model.addAttribute("todasAsUrgencias", Urgencia.values());
        return "triagens";
    }

    @GetMapping("/portal/triagens/{id}")
    public String detalhe(@AuthenticationPrincipal UsuarioPrincipal principal,
                          @PathVariable Long id,
                          Model model) {

        model.addAttribute("triagem", triagemService.buscarPorId(principal, id));
        return "triagem";
    }

    @GetMapping("/portal/pets/{id}/triagens")
    public String historicoDoPet(@AuthenticationPrincipal UsuarioPrincipal principal,
                                 @PathVariable Long id,
                                 Model model) {

        model.addAttribute("pet", petService.buscarPorId(principal, id));
        model.addAttribute("triagens", triagemService.listarPorPet(principal, id));
        return "pet-triagens";
    }
}
