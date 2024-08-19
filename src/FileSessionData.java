public class FileSessionData implements SessionBehaviour{
    private String fileName;
    private int sequenceNumber;
    private boolean isLastPiece;

    public FileSessionData(String fileName) {
        this.fileName = fileName;
        sequenceNumber = 0;
        isLastPiece = false;
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public void increaseSequenceNumber() {
        sequenceNumber++;
    }

    @Override
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public DataTypes getDataType() {
        return DataTypes.FILE;
    }

    @Override
    public boolean isLastPiece() {
        return isLastPiece;
    }

    @Override
    public void setIsLastPiece() {
        isLastPiece = true;
    }
}
