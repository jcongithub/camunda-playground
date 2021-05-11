package com.example.workflow;

import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FixedWidthFileReader {
    @Data
    @Builder
    class ReadStatus {
        int startLineNumber;
        int endLineNumber;
        int numberLinesRead;
        boolean succeeded;
    }

    interface HeaderReader {
        ReadStatus read(BufferedReader reader, int currentLineNumber) throws IOException;
    }
    interface RecordReader {
        ReadStatus read(BufferedReader reader, int currentLineNumber) throws IOException;
        List<List<String>> getRecords();
    }
    interface FooterReader{
        ReadStatus read(BufferedReader reader, int currentLineNumber);
    }

    private HeaderReader headerReader;
    private RecordReader recordReader;
    private FooterReader footerReader;
    private List<List<String>> allRecords = new LinkedList<>();

    public FixedWidthFileReader(HeaderReader headerReader, RecordReader recordReader, FooterReader footerReader) {
        this.headerReader = headerReader;
        this.recordReader = recordReader;
        this.footerReader = footerReader;
    }

    void read(BufferedReader reader) throws IOException {
        ReadStatus status = headerReader.read(reader, 0);
        //Log.info("Read header: {}", status);
        while(status.succeeded) {
            status = recordReader.read(reader, status.getEndLineNumber());
            if(status.succeeded) {
                List<List<String>> records = recordReader.getRecords();
                allRecords.addAll(records);
                status = footerReader.read(reader, status.getEndLineNumber());
                if(status.succeeded) {
                    status = headerReader.read(reader, status.getEndLineNumber());
                }
            } else {
                throw new IOException(String.format("Cannot read the record at the line: %d", status.getEndLineNumber()));
            }
        }
    }

    public static class MultiLinesHeaderReader implements HeaderReader {
        private List<String> headerLinePatterns;
        private int headerLookFor = 0;
        private int currentLineNumber;
        private int startLineNumber;

        public MultiLinesHeaderReader(List<String> headerLinePatterns) {
            this.headerLinePatterns = headerLinePatterns;
        }

        @Override
        public ReadStatus read(BufferedReader reader, int currentLineNumber) throws IOException{
            this.currentLineNumber = currentLineNumber;
            this.startLineNumber = currentLineNumber;
            String line = null;

            while((headerLookFor < headerLinePatterns.size()) && (line = reader.readLine()) != null) {
                String pattern = headerLinePatterns.get(headerLookFor);
                if(line.matches(pattern)) {
                    headerLookFor++;
                } else {
                    throw new IOException(String.format("Not a header line at the line:%. Expected: %s Actual line: %", currentLineNumber, pattern, line));
                }
            }
            if(headerLookFor == headerLinePatterns.size()) {
                return ReadStatus.builder().startLineNumber(startLineNumber).endLineNumber(currentLineNumber).succeeded(true).build();
            } else {
                throw new IOException(String.format("Cannot read completed headers from the line: %d to the end", startLineNumber));
            }
        }
    }

    public static class FixedWidthRecordReader implements RecordReader {
        private List<Integer> fieldWidths;
        private int recordWidth;
        private List<List<String>> records;
        private int currentLineNumber;
        private int startLineNumber;

        public FixedWidthRecordReader(List<Integer> fieldWidths) {
            this.fieldWidths = fieldWidths;
            this.recordWidth = this.fieldWidths.stream().mapToInt(x->x).sum();
        }

        @Override
        public ReadStatus read(BufferedReader reader, int currentLineNumber) throws IOException{
            this.currentLineNumber = currentLineNumber;
            this.startLineNumber = currentLineNumber;
            String line = null;

            while((line = reader.readLine()) != null) {
                if(line.length() < this.recordWidth) {
                    throw new IOException(String.format("Cannot read a record at the line: %d", currentLineNumber));
                } else {
                    int pos = 0;
                    List<String> record = new ArrayList<>();
                    for (int i = 0; i < fieldWidths.size(); i++){
                        int width = fieldWidths.get(i);
                        record.add(line.substring(pos, pos+width));
                        pos = pos + width;
                    }
                    records.add(record);
                }
            }
            return ReadStatus.builder().succeeded(true).startLineNumber(startLineNumber).endLineNumber(currentLineNumber).build();
        }

        @Override
        public List<List<String>> getRecords() {
            return  records;
        }
    }

    public static class MultiLineFooterReader implements FooterReader {
        @Override
        public ReadStatus read(BufferedReader reader, int currentLineNumber) {
            return null;
        }
    }

    public static class GspCashPostingReportReader extends FixedWidthFileReader{
        private static List<String> headerPatterns = Arrays.asList(
            "---------------.*", "AAA BBB CCC .*", "--------.*"
        );
        private static List<Integer> fieldWidths = Arrays.asList(3, 5, 6);

        private static MultiLinesHeaderReader headerReader = new MultiLinesHeaderReader(headerPatterns);
        private static FixedWidthRecordReader recordReader = new FixedWidthRecordReader(fieldWidths);
        private static FooterReader footerReader = null;

        public GspCashPostingReportReader() {
            super(headerReader, recordReader, footerReader);
        }
    }


    public static void main(String[] args) throws IOException {
        String fileName = "gsp.txt";
        GspCashPostingReportReader reader = new GspCashPostingReportReader();
        ClassLoader classLoader = reader.getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(fileName);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(streamReader)) {
                reader.read(br);


        }
    }
}
