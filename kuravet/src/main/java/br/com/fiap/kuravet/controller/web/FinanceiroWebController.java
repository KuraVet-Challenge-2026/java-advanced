package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.dto.financeiro.LinhaFaturamentoDTO;
import br.com.fiap.kuravet.dto.financeiro.PagamentoForm;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.ConsultaService;
import br.com.fiap.kuravet.service.FinanceiroService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

// Tela Financeiro do portal (/portal/** exige VETERINARIO). Pagamento e faturamento passam pelos
// packages PL/SQL; erros ORA-200xx chegam aqui como RegraDeNegocioException e viram mensagem na tela.
@Controller
public class FinanceiroWebController {

    private final FinanceiroService financeiroService;
    private final ConsultaService consultaService;

    public FinanceiroWebController(FinanceiroService financeiroService, ConsultaService consultaService) {
        this.financeiroService = financeiroService;
        this.consultaService = consultaService;
    }

    @GetMapping("/portal/financeiro")
    public String financeiro(@AuthenticationPrincipal UsuarioPrincipal principal,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                             Model model) {
        if (!model.containsAttribute("pagamentoForm")) {
            model.addAttribute("pagamentoForm", new PagamentoForm());
        }
        carregarTela(principal, inicio, fim, model);
        return "financeiro";
    }

    @PostMapping("/portal/financeiro/pagamentos")
    public String registrarPagamento(@AuthenticationPrincipal UsuarioPrincipal principal,
                                     @ModelAttribute("pagamentoForm") @Valid PagamentoForm form,
                                     BindingResult resultado,
                                     Model model,
                                     RedirectAttributes redirect) {

        if (!resultado.hasErrors()) {
            try {
                long idPagamento = financeiroService.registrarPagamento(form);
                redirect.addFlashAttribute("sucesso", "Pagamento nº " + idPagamento + " registrado.");
                return "redirect:/portal/financeiro";
            } catch (RegraDeNegocioException e) {
                // Ex.: consulta nao realizada ou valor invalido (regra do PL/SQL). Volta ao formulario.
                resultado.reject("regraDeNegocio", e.getMessage());
            }
        }

        carregarTela(principal, null, null, model);
        return "financeiro";
    }

    // Periodo padrao: ano corrente. Erro no filtro aparece junto da tabela, sem pagina de erro.
    private void carregarTela(UsuarioPrincipal principal, LocalDate inicio, LocalDate fim, Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioFiltro = inicio != null ? inicio : hoje.withDayOfYear(1);
        LocalDate fimFiltro = fim != null ? fim : hoje.withDayOfYear(hoje.lengthOfYear());

        List<LinhaFaturamentoDTO> linhas = List.of();
        try {
            linhas = financeiroService.faturamento(inicioFiltro, fimFiltro);
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erroFiltro", e.getMessage());
        }

        model.addAttribute("consultasRealizadas", consultaService.listarPorStatus(principal, StatusConsulta.REALIZADA));
        model.addAttribute("tiposPagamento", PagamentoForm.TIPOS_PAGAMENTO);
        model.addAttribute("inicio", inicioFiltro);
        model.addAttribute("fim", fimFiltro);
        model.addAttribute("linhas", linhas);
    }
}
