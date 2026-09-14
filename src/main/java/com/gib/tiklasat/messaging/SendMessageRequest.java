package com.gib.tiklasat.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {

    /** Kime yazılıyor. Sohbet yoksa ilk mesajda oluşturulur. */
    @NotNull(message = "Alıcı zorunludur.")
    private UUID recipientId;

    @NotBlank(message = "Mesaj boş olamaz.")
    @Size(max = 2000, message = "Mesaj en fazla 2000 karakter olabilir.")
    private String body;
}
