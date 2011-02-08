package com.bloatit.framework.xcgiserver.mime.decoders;

import java.util.Arrays;

/**
 * <p>A blank decoder</p>
 * <p>One byte of encoded data => the same byte of decoded data</p> 
 */
public class MimeBinaryDecoder implements MimeDecoder {

    @Override
    public byte[] decode(byte[] b, int offset, int length) {
        return Arrays.copyOfRange(b, offset, offset + length);
    }

    @Override
    public int decodeStep() {
        return 1;
    }

}