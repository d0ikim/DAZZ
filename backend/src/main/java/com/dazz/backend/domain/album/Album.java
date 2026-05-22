package com.dazz.backend.domain.album;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 앨범 Aggregate Root.
 * albumReview: 곡 해석 및 해설 (도슨트 관점의 앨범 코멘트).
 */
@Getter
public class Album {

    private final Long id;
    private final String title;
    private final LocalDate releaseDate;
    private final String coverImageUrl;
    private final String albumReview;

    @Builder
    private Album(Long id, String title, LocalDate releaseDate,
                  String coverImageUrl, String albumReview) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.coverImageUrl = coverImageUrl;
        this.albumReview = albumReview;
    }
}
