

public interface SessionBehaviour {
    public void increaseSequenceNumber();
    public int getSequenceNumber();
    public DataTypes getDataType();
    public boolean isLastPiece();
    public void setIsLastPiece();
    public String getFilename();
} 
