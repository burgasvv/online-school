package org.burgas.mediaservice.dao.image;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.mediaservice.dao.Dao;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image", schema = "public")
public class Image implements Dao {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", columnDefinition = "varchar")
    private String name;

    @Column(name = "content_type", columnDefinition = "varchar")
    private String contentType;

    @Column(name = "preview", columnDefinition = "boolean")
    private Boolean preview;

    @Column(name = "size", columnDefinition = "bigint")
    private Long size;

    @Column(name = "data", columnDefinition = "bytea")
    private byte[] data;
}
