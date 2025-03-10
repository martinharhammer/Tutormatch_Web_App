package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    @NotNull
    private String chatRoomId;
    @NotNull
    private Long senderId;
    @NotNull
    private Long recipientId;
    @NotNull
    @Size(max = 500)
    private String content;
    @NotNull
    private Date timestamp;
}