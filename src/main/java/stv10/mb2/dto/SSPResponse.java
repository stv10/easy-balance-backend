package stv10.mb2.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SSPResponse<T> {
    private int pageIndex;
    private int pageSize;
    private long totalItems;
    private List<T> data;
}
