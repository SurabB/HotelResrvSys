package com.backend.hotelReservationSystem.entity.embeddable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Image {
    @Column(name = "image_type")
    private String imageType;

    @Lob
    @Column(name="image",columnDefinition = "MEDIUMBLOB")
    @Basic(fetch = FetchType.LAZY)
    private byte[] image;
}
