package org.example.sudobeats.mapper;

import org.example.sudobeats.dto.response.MoveResponse;
import org.example.sudobeats.entity.Move;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MoveMapper {

    public MoveResponse toResponse(Move move) {
        if (move == null) return null;
        return new MoveResponse(
                move.getId(),
                move.getGame().getId(),
                move.getRow(),
                move.getCol(),
                move.getValue(),
                move.getTimestampFromStart(),
                move.isCorrect()
        );
    }

    public List<MoveResponse> toResponseList(List<Move> moves) {
        return moves.stream().map(this::toResponse).toList();
    }
}
