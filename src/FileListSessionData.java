

import java.util.*;

public class FileListSessionData implements SessionBehaviour {
    Queue<String> fileNames;
    private int sequenceNumber;
    private boolean isLastPiece;
    

    public FileListSessionData(ArrayDeque<String> fileNames) {
        this.fileNames = fileNames;
        sequenceNumber = 1;
        isLastPiece = false;
    }

    @Override
    public void increaseSequenceNumber() {
        sequenceNumber++;
        fileNames.remove();
        if (fileNames.size()==1) {
            setIsLastPiece();
        }
    }

    @Override
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public DataTypes getDataType() {
        return DataTypes.FILELIST;
    }

    @Override
    public boolean isLastPiece() {
        return isLastPiece;
    }

    @Override
    public void setIsLastPiece() {
        isLastPiece = true;
    }

    @Override
    public String getFilename() {
        return fileNames.peek();
    }


}
