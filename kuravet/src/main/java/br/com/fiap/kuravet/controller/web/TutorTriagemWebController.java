package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.model.Triagem;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.PetService;
import br.com.fiap.kuravet.service.TriagemService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Triagem por IA na area do tutor (/tutor/** exige TUTOR). Regras ficam no TriagemService.
@Controller
public class TutorTriagemWebController {

    private final TriagemService triagemService;
    private final PetService petService;

    public TutorTriagemWebController(TriagemService triagemService, PetService petService) {
        this.triagemService = triagemService;
        this.petService = petService;
    }

    @GetMapping("/tutor/triagens")
    public String historico(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("triagens", triagemService.listar(principal));
        return "tutor-triagens";
    }

    @GetMapping("/tutor/triagens/nova")
    public String formulario(@AuthenticationPrincipal UsuarioPrincipal principal,
                             @RequestParam(required = false) Long idPet,
                             Model model) {
        TriagemForm form = new TriagemForm();
        form.setIdPet(idPet);
        model.addAttribute("triagemForm", form);
        model.addAttribute("pets", petService.listar(principal));
        return "tutor-triagem-nova";
    }

    // Redireciona para o resultado (POST/redirect/GET): atualizar a pagina nao refaz a triagem nem chama a LLM de novo.
    @PostMapping("/tutor/triagens")
    public String realizar(@AuthenticationPrincipal UsuarioPrincipal principal,
                           @ModelAttribute("triagemForm") @Valid TriagemForm form,
                           BindingResult resultado,
                           Model model) {

        if (resultado.hasErrors()) {
            model.addAttribute("pets", petService.listar(principal));
            return "tutor-triagem-nova";
        }

        Triagem triagem = triagemService.realizar(principal, form.paraDTO());
        return "redirect:/tutor/triagens/" + triagem.getIdTriagem();
    }

    @GetMapping("/tutor/triagens/{id}")
    public String resultado(@AuthenticationPrincipal UsuarioPrincipal principal,
                            @PathVariable Long id,
                            Model model) {

        Triagem triagem = triagemService.buscarPorId(principal, id);
        model.addAttribute("triagem", triagem);
        model.addAttribute("urgente", triagem.getUrgencia().sugereTeleconsultaPrioritaria());
        return "tutor-triagem-resultado";
    }
}
