package miniplc0java.analyser;

public class TypeAndPos {
    //0:unknown 1:global 2:local 3:param
    int type;
    //1:在全局/局部/参数中的位置
    int position;

    public TypeAndPos(int type, int position) {
        this.type = type;
        this.position = position;
    }
}
