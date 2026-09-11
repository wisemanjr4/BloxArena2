# Taste Profile

## Communication
- Communicates in Japanese; sends terse, casual one-line requests (e.g. 「アップデート処理お願い」) and expects the agent to infer the full task scope and execute it end-to-end without asking for confirmation. Confidence: 0.8

## Workflow
- 「アップデート処理」(update process) means the standard release routine for this project: build the jar with Maven (`mvn -B package -DskipTests`), then `git add -A`, commit with a Japanese message summarizing the changes, and `git push` to GitHub — run proactively once work is done, without being asked for each step. Confidence: 0.7
- Deliver completion reports as a concise Japanese summary with section headers and bullet lists describing what changed (commit hash + GitHub status up front). Confidence: 0.6

## Style (project conventions)
- The in-game Japanese message style system the user accepted: symbol vocabulary ⚔ kills / ★ headings & victory / ⚡ ULT / ✦ rounds / ⏱ timeout / » separators; team colors RED=§c, BLUE=§9; skill activation messages themed by element (fire §6, ice §b, dark §5, lightning §e); cool/battle-like tone. Keep future text/UI work consistent with this system. Confidence: 0.6
