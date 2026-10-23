package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Area do tutor no portal (/tutor/** exige perfil TUTOR). A regra de cancelamento vive no ConsultaService.
@Controller
public class TutorWebController {

    private final ConsultaService consultaService;

    public TutorWebController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping("/tutor/consultas")
    public String consultas(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        List<Consulta> consultas = consultaService.listar(principal);

        Set<Long> cancelaveis = consultas.stream()
                .filter(consulta -> consultaService.podeCancelar(principal, consulta))
                .map(Consulta::getIdConsulta)
                .collect(Collectors.toSet());

        model.addAttribute("consultas", consultas);
        model.addAttribute("cancelaveis", cancelaveis);
        return "tutor-consultas";
    }

    @PostMapping("/tutor/consultas/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal UsuarioPrincipal principal,
                           @PathVariable Long id,
                           RedirectAttributes redirect) {

        Consulta consulta = consultaService.cancelar(principal, id);
        redirect.addFlashAttribute("sucesso", "Consulta de " + consulta.getPet().getNome() + " cancelada.");
        return "redirect:/tutor/consultas";
    }
}
