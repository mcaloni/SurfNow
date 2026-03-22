package com.surfnow.api;

import com.surfnow.api.dto.BeachResponse;
import com.surfnow.application.beach.GetBeachScoreUseCase;
import com.surfnow.application.beach.ListBestBeachesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beaches")
@RequiredArgsConstructor
public class BeachController {

    private final ListBestBeachesUseCase listBestBeachesUseCase;
    private final GetBeachScoreUseCase getBeachScoreUseCase;

    /**
     * Lista praias ordenadas por score (melhor primeiro).
     * Filtro opcional por estado: ?state=SP
     */
    @GetMapping
    public List<BeachResponse> listBeaches(@RequestParam(required = false) String state) {
        return listBestBeachesUseCase.execute(state).stream()
                .map(BeachResponse::from)
                .toList();
    }

    /**
     * Retorna score e condições atuais de uma praia específica.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BeachResponse> getBeach(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(BeachResponse.from(getBeachScoreUseCase.execute(id)));
        } catch (GetBeachScoreUseCase.BeachNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
