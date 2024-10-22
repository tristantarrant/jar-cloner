package io.github.chirino.jarcloner;

import java.util.jar.Attributes;

public class Entry {
    public String name;
    public String comment;
    public long time;
    public byte[] extra;
    public int method;
    public long crc;
    public long size;
}