package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.controller.web.PetForm;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.PetService;
import br.com.fiap.kuravet.service.TutorService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/portal/pets")
public class PetWebController {

    private final PetService petService;
    private final TutorService tutorService;

    public PetWebController(PetService petService, TutorService tutorService) {
        this.petService = petService;
        this.tutorService = tutorService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("pets", petService.listar(principal));
        return "pets";
    }

    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("petForm", new PetForm());
        model.addAttribute("tutores", tutorService.listar());
        model.addAttribute("edicao", false);
        return "pet-formulario";
    }

    @PostMapping
    public String cadastrar(@ModelAttribute("petForm") @Valid PetForm form,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("tutores", tutorService.listar());
            model.addAttribute("edicao", false);
            return "pet-formulario";
        }

        Pet pet = petService.cadastrar(form.getIdTutor(), form.paraDTO());
        redirect.addFlashAttribute("sucesso", "Pet " + pet.getNome() + " cadastrado com sucesso.");
        return "redirect:/portal/pets";
    }

    @GetMapping("/{id}/editar")
    public String formularioEdicao(@AuthenticationPrincipal UsuarioPrincipal principal,
                                   @PathVariable Long id,
                                   Model model) {

        model.addAttribute("petForm", PetForm.de(petService.buscarPorId(principal, id)));
        model.addAttribute("tutores", tutorService.listar());
        model.addAttribute("edicao", true);
        return "pet-formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(@AuthenticationPrincipal UsuarioPrincipal principal,
                            @PathVariable Long id,
                            @ModelAttribute("petForm") @Valid PetForm form,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("tutores", tutorService.listar());
            model.addAttribute("edicao", true);
            return "pet-formulario";
        }

        Pet pet = petService.atualizarComTutor(principal, id, form.getIdTutor(), form.paraDTO());
        redirect.addFlashAttribute("sucesso", "Dados de " + pet.getNome() + " atualizados.");
        return "redirect:/portal/pets";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal UsuarioPrincipal principal,
                          @PathVariable Long id,
                          RedirectAttributes redirect) {

        Pet pet = petService.buscarPorId(principal, id);
        petService.excluir(principal, id);
        redirect.addFlashAttribute("sucesso", "Pet " + pet.getNome() + " excluido.");
        return "redirect:/portal/pets";
    }
}