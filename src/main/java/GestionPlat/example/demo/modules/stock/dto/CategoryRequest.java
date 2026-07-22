package GestionPlat.example.demo.modules.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    private String name;
    private String description;

    @JsonProperty("parentId")
    @JsonAlias("parent_id")
    private Long parentId;

    @JsonProperty("parentCategory")
    @JsonAlias("parent")
    private ParentRef parentCategory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentRef {
        private Long id;
    }
}
