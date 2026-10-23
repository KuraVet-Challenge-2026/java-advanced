package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import br.com.fiap.kuravet.service.PetService;
import br.com.fiap.kuravet.service.VeterinarioService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Area do tutor no portal (/tutor/** exige perfil TUTOR). A regra de cancelamento vive no ConsultaService.
@Controller
public class TutorWebController {

    private final ConsultaService consultaService;
    private final PetService petService;
    private final VeterinarioService veterinarioService;

    public TutorWebController(ConsultaService consultaService,
                              PetService petService,
                              VeterinarioService veterinarioService) {
        this.consultaService = consultaService;
        this.petService = petService;
        this.veterinarioService = veterinarioService;
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

    @GetMapping("/tutor/consultas/nova")
    public String formularioSolicitacao(@AuthenticationPrincipal UsuarioPrincipal principal,
                                        @RequestParam(required = false) Long idPet,
                                        Model model) {
        SolicitacaoConsultaForm form = new SolicitacaoConsultaForm();
        form.setIdPet(idPet);
        model.addAttribute("solicitacaoForm", form);
        carregarOpcoes(principal, model);
        return "tutor-solicitar-consulta";
    }

    @PostMapping("/tutor/consultas")
    public String solicitar(@AuthenticationPrincipal UsuarioPrincipal principal,
                            @ModelAttribute("solicitacaoForm") @Valid SolicitacaoConsultaForm form,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes redirect) {

        if (!resultado.hasErrors()) {
            try {
                Consulta consulta = consultaService.solicitarTeleconsulta(principal, form.paraDTO());
                redirect.addFlashAttribute("sucesso", "Teleconsulta de " + consulta.getPet().getNome()
                        + " solicitada. Aguarde a aprovação da clínica.");
                return "redirect:/tutor/consultas";
            } catch (RegraDeNegocioException e) {
                // Ex.: o pet ja tem consulta ativa na data. Volta ao formulario com a mensagem, sem pagina de erro.
                resultado.reject("regraDeNegocio", e.getMessage());
            }
        }

        carregarOpcoes(principal, model);
        return "tutor-solicitar-consulta";
    }

    private void carregarOpcoes(UsuarioPrincipal principal, Model model) {
        model.addAttribute("pets", petService.listar(principal));
        model.addAttribute("veterinarios", veterinarioService.listar());
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
