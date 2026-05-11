package org.example.sudobeats.mapper;

import org.example.sudobeats.dto.response.GameResponse;
import org.example.sudobeats.entity.SudokuGame;
import org.example.sudobeats.repository.MoveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameMapper {

    private final MoveRepository moveRepository;

    public GameResponse toResponse(SudokuGame game) {
        if (game == null) return null;

        long totalMoves   = moveRepository.countByGameId(game.getId());
        long correctMoves = moveRepository.countByGameIdAndCorrect(game.getId(), true);

        return new GameResponse(
                game.getId(),
                game.getUser().getId(),
                game.getUser().getUsername(),
                game.getInitialBoard(),
                game.getCurrentBoard(),
                game.getDifficulty(),
                game.getStatus(),
                game.getStartedAt(),
                game.getCompletedAt(),
                totalMoves,
                correctMoves
        );
    }
}
