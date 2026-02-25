package requests;

import java.util.Map;
import java.util.Objects;

public record Response(int code, String json) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Response response = (Response) o;
        return code() == response.code() && Objects.equals(json(), response.json());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code(), json());
    }
}
