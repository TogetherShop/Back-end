package com.togethershop.backend.service;
import com.togethershop.backend.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QRCodeService {

    public byte[] generateQRCode(String data) throws Exception {
        return QRCodeGenerator.generateQRCodeImage(data, 300, 300);
    }
}
