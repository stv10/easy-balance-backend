package stv10.mb2.dto;

import lombok.Data;
import java.util.List;

@Data
public class SSPRequest {
    private int pageIndex;
    private int pageSize;
    private List<SSPFilter> filters;
}
