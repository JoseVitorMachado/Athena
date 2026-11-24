package br.ufla.athena.projetosacesso.docs;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Hidden
@Controller
public class DocumentacaoController {

    @GetMapping("/api/v1/autenticacao/docs")
    public String documentacao() {
        return "redirect:/api/v1/autenticacao/docs/index.html";
    }
}
