package com.dazz.backend.api.musician;

import com.dazz.backend.api.common.ApiResponse;
import com.dazz.backend.api.musician.dto.MusicianInsightResponse;
import com.dazz.backend.api.musician.mapper.MusicianInsightMapper;
import com.dazz.backend.application.musician.MusicianInsightResult;
import com.dazz.backend.application.musician.MusicianQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/musicians")
@RequiredArgsConstructor
@Validated
@Tag(name = "Musician", description = "뮤지션 인사이트 API")
public class MusicianController {

    private final MusicianQueryService musicianQueryService;

    @GetMapping("/{musicianId}/insights")
    @Operation(summary = "뮤지션 인사이트 조회", description = "뮤지션 프로필 + 협업 네트워크를 한 번에 조회합니다.")
    public ResponseEntity<ApiResponse<MusicianInsightResponse>> getInsight(
            @PathVariable Long musicianId,
            @RequestParam(defaultValue = "true") boolean includeNetwork,
            @RequestParam(defaultValue = "1") @Min(1) @Max(2) int depth
    ) {
        MusicianInsightResult result = musicianQueryService.getInsight(musicianId, includeNetwork, depth);
        return ResponseEntity.ok(ApiResponse.ok(MusicianInsightMapper.toResponse(result)));
    }
}
