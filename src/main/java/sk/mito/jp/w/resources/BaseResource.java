package sk.mito.jp.w.resources;

public class BaseResource {
    /**
     * @param id is a number or string id
     * @return number if really a number otherwise null
     */
    protected Long checkIfIdIsNumber(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
