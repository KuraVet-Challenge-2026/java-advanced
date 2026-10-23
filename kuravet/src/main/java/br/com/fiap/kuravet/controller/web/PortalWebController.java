package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.config.RedirecionamentoPorPerfilHandler;
import br.com.fiap.kuravet.dto.consulta.DiagnosticoRequestDTO;
import br.com.fiap.kuravet.dto.consulta.RecusaRequestDTO;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PortalWebController {

    private final ConsultaService consultaService;

    private final TriagemService triagemService;

    public PortalWebController(ConsultaService consultaService, TriagemService triagemService) {
        this.consultaService = consultaService;
        this.triagemService = triagemService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Quem nao esta logado e levado ao /login pela seguranca antes de chegar aqui.
    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return "redirect:" + RedirecionamentoPorPerfilHandler.destino(principal.getPerfil());
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }


    @GetMapping("/portal/painel")
    public String painel(Model model) {
        model.addAttribute("totalSolicitadas", consultaService.contarPorStatus(StatusConsulta.SOLICITADA));
        model.addAttribute("totalAgendadas", consultaService.contarPorStatus(StatusConsulta.AGENDADA));
        model.addAttribute("totalRealizadas", consultaService.contarPorStatus(StatusConsulta.REALIZADA));
        model.addAttribute("totalRecusadas", consultaService.contarPorStatus(StatusConsulta.RECUSADA));
        model.addAttribute("totalTriagensUrgentes",
                triagemService.contarUrgentesDesde(LocalDateTime.now().minusDays(7)));
        return "painel";
    }


    @GetMapping("/portal/solicitacoes")
    public String solicitacoes(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("solicitacoes",
                consultaService.listarPorStatus(principal, StatusConsulta.SOLICITADA));
        return "solicitacoes";
    }

    @PostMapping("/portal/solicitacoes/{id}/aprovar")
    public String aprovar(@PathVariable Long id, RedirectAttributes redirect) {
        Consulta consulta = consultaService.aprovarSolicitacao(id);
        redirect.addFlashAttribute("sucesso",
                "Teleconsulta de " + consulta.getPet().getNome() + " aprovada e agendada.");
        return "redirect:/portal/solicitacoes";
    }

    @GetMapping("/portal/solicitacoes/{id}/recusar")
    public String formularioRecusa(@AuthenticationPrincipal UsuarioPrincipal principal,
                                   @PathVariable Long id,
                                   Model model) {
        model.addAttribute("consulta", consultaService.buscarPorId(principal, id));
        model.addAttribute("recusaForm", new RecusaRequestDTO());
        return "recusa";
    }

    @PostMapping("/portal/solicitacoes/{id}/recusar")
    public String recusar(@AuthenticationPrincipal UsuarioPrincipal principal,
                          @PathVariable Long id,
                          @ModelAttribute("recusaForm") @Valid RecusaRequestDTO form,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("consulta", consultaService.buscarPorId(principal, id));
            return "recusa";
        }

        Consulta consulta = consultaService.recusarSolicitacao(id, form.getMotivo());
        redirect.addFlashAttribute("sucesso",
                "Solicitação de " + consulta.getPet().getNome() + " recusada, com o motivo registrado.");
        return "redirect:/portal/solicitacoes";
    }


    @GetMapping("/portal/consultas")
    public String consultas(@AuthenticationPrincipal UsuarioPrincipal principal,
                            @RequestParam(required = false) StatusConsulta status,
                            Model model) {

        List<Consulta> consultas = (status == null)
                ? consultaService.listar(principal)
                : consultaService.listarPorStatus(principal, status);

        model.addAttribute("consultas", consultas);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("todosOsStatus", StatusConsulta.values());
        return "consultas";
    }

    @GetMapping("/portal/consultas/{id}")
    public String detalhe(@AuthenticationPrincipal UsuarioPrincipal principal,
                          @PathVariable Long id,
                          Model model) {

        model.addAttribute("consulta", consultaService.buscarPorId(principal, id));

        if (!model.containsAttribute("diagnosticoForm")) {
            model.addAttribute("diagnosticoForm", new DiagnosticoRequestDTO());
        }

        return "consulta";
    }

    @PostMapping("/portal/consultas/{id}/diagnostico")
    public String emitirDiagnostico(@AuthenticationPrincipal UsuarioPrincipal principal,
                                    @PathVariable Long id,
                                    @ModelAttribute("diagnosticoForm") @Valid DiagnosticoRequestDTO form,
                                    BindingResult resultado,
                                    Model model,
                                    RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("consulta", consultaService.buscarPorId(principal, id));
            return "consulta";
        }

        consultaService.realizarConsultaComDiagnostico(id, form.getDiagnostico());
        redirect.addFlashAttribute("sucesso", "Diagnóstico registrado. Consulta encerrada.");
        return "redirect:/portal/consultas/" + id;
    }
}