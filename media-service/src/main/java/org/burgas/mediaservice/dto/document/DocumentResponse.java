package org.burgas.mediaservice.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.mediaservice.dto.Response;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse implements Response {

    private UUID id;
    private String name;
    private String contentType;
    private Long size;
}
