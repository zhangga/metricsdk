package test.metrics4j;

public class TestObj {

    private long id;
    private byte[] data;

    public TestObj() {

    }

    public TestObj(long id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
