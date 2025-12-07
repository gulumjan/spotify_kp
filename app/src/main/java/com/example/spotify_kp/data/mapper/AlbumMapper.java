package com.example.spotify_kp.data.mapper;

import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.remote.dto.AlbumDto;
import com.example.spotify_kp.data.remote.dto.ArtistDto;
import com.example.spotify_kp.data.remote.dto.ImageDto;

import java.util.ArrayList;
import java.util.List;

public class AlbumMapper {

    public static AlbumEntity toEntity(AlbumDto dto) {
        if (dto == null) {
            return null;
        }

        AlbumEntity entity = new AlbumEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getName());

        // Извлекаем имя первого артиста
        if (dto.getArtists() != null && !dto.getArtists().isEmpty()) {
            entity.setArtist(dto.getArtists().get(0).getName());
        } else {
            entity.setArtist("Unknown Artist");
        }

        // Извлекаем год из release_date (2011-01-01 → 2011)
        if (dto.getReleaseDate() != null && dto.getReleaseDate().length() >= 4) {
            entity.setYear(dto.getReleaseDate().substring(0, 4));
        } else {
            entity.setYear("Unknown");
        }

        // Извлекаем URL обложки (берём первое изображение)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            entity.setCoverUrl(dto.getImages().get(0).getUrl());
        } else {
            entity.setCoverUrl("");
        }

        entity.setTotalTracks(dto.getTotalTracks());
        entity.setReleaseDate(dto.getReleaseDate());
        entity.setSpotifyId(dto.getId());
        entity.setGenre("Electronic"); // Временно, т.к. genres пустой
        entity.setCreatedAt(System.currentTimeMillis());

        return entity;
    }

    public static List<AlbumEntity> toEntityList(List<AlbumDto> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<AlbumEntity> entities = new ArrayList<>();
        for (AlbumDto dto : dtoList) {
            AlbumEntity entity = toEntity(dto);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }
}