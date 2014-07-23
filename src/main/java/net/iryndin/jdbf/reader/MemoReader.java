package net.iryndin.jdbf.reader;

import net.iryndin.jdbf.core.MemoFileHeader;
import net.iryndin.jdbf.core.MemoRecord;
import net.iryndin.jdbf.util.BitUtils;
import net.iryndin.jdbf.util.IOUtils;
import net.iryndin.jdbf.util.JdbfUtils;

import java.io.*;

/**
 * Reader of memo files (tested of *.FPT files - Visual FoxPro)
 */
public class MemoReader implements Closeable {

    private ByteArrayInputStream memoInputStream;
    private MemoFileHeader memoHeader;

    public MemoReader(File memoFile) throws IOException {
        this(new FileInputStream(memoFile));
    }

    public MemoReader(InputStream inputStream) throws IOException {
        this.memoInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
        readMetadata();
    }

    private void readMetadata() throws IOException {
        byte[] headerBytes = new byte[JdbfUtils.MEMO_HEADER_LENGTH];
        memoInputStream.read(headerBytes);
        this.memoHeader = MemoFileHeader.create(headerBytes);
    }

    @Override
    public void close() throws IOException {
        if (memoInputStream != null) {
            memoInputStream.close();
        }
    }

    public MemoFileHeader getMemoHeader() {
        return memoHeader;
    }

    public MemoRecord read(int offsetInBlocks) throws IOException {
        memoInputStream.reset();
        memoInputStream.skip(memoHeader.getBlockSize()*offsetInBlocks);
        byte[] recordHeader = new byte[8];
        memoInputStream.read(recordHeader);
        int memoRecordLength = BitUtils.makeInt(recordHeader[7], recordHeader[6], recordHeader[5], recordHeader[4]);
        byte[] recordBody = new byte[memoRecordLength];
        memoInputStream.read(recordBody);

        return new MemoRecord(recordHeader, recordBody, memoHeader.getBlockSize(), offsetInBlocks);
    }
}