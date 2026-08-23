package com.avantt_backend.controller;

import com.avantt_backend.dto.ExampleDto;
import com.avantt_backend.service.ExampleService;
import com.avantt_backend.docs.ApiDocsTexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.avantt_backend.config.ApiPaths;

import java.util.List;

/**
 * Exemplo de Controller REST para começar a estrutura MVC.
 */
@RestController
@RequestMapping(ApiPaths.EXAMPLE)
@Tag(name = ApiDocsTexts.TAG_EXAMPLE, description = ApiDocsTexts.TAG_EXAMPLE_DESC)
public class HomeController {

    private final ExampleService service;

    @Autowired
    public HomeController(ExampleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = ApiDocsTexts.EXAMPLE_LIST_SUMMARY, description = ApiDocsTexts.EXAMPLE_LIST_DESCRIPTION)
    @Parameters({
            @Parameter(name = ApiDocsTexts.PARAM_PAGE_NAME, in = ParameterIn.QUERY, description = ApiDocsTexts.PARAM_PAGE_DESC, required = false,
                    schema = @Schema(type = "integer", example = ApiDocsTexts.PARAM_PAGE_EX)),
            @Parameter(name = ApiDocsTexts.PARAM_SIZE_NAME, in = ParameterIn.QUERY, description = ApiDocsTexts.PARAM_SIZE_DESC, required = false,
                    schema = @Schema(type = "integer", example = ApiDocsTexts.PARAM_SIZE_EX)),
            @Parameter(name = ApiDocsTexts.PARAM_SORT_NAME, in = ParameterIn.QUERY, description = ApiDocsTexts.PARAM_SORT_DESC, required = false,
                    schema = @Schema(type = "string", example = ApiDocsTexts.PARAM_SORT_EX)),
            @Parameter(name = ApiDocsTexts.PARAM_NAME_NAME, in = ParameterIn.QUERY, description = ApiDocsTexts.PARAM_NAME_DESC, required = false,
                    schema = @Schema(type = "string", example = ApiDocsTexts.PARAM_NAME_EX))
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = ApiDocsTexts.RESP_200),
            @ApiResponse(responseCode = "400", description = ApiDocsTexts.RESP_400),
            @ApiResponse(responseCode = "500", description = ApiDocsTexts.RESP_500)
    })
    public List<ExampleDto> list(
            @RequestParam(name = ApiDocsTexts.PARAM_PAGE_NAME, required = false, defaultValue = ApiDocsTexts.PARAM_PAGE_EX) Integer page,
            @RequestParam(name = ApiDocsTexts.PARAM_SIZE_NAME, required = false, defaultValue = ApiDocsTexts.PARAM_SIZE_EX) Integer size,
            @RequestParam(name = ApiDocsTexts.PARAM_SORT_NAME, required = false) String sort,
            @RequestParam(name = ApiDocsTexts.PARAM_NAME_NAME, required = false) String name
    ) {
        // parâmetros atualmente não alteram o comportamento; podem ser usados futuramente no service
        return service.findAll();
    }
}
