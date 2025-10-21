# Recommended Actions for Refactored Code

This checklist provides actionable steps to ensure the refactored code achieves full parity with the legacy implementation and is ready for production use.

---

## ✅ Phase 1: Verification Testing (Priority: Critical)

### Integration Tests
- [ ] **Test Production Phase**
  - [ ] Verify base production (1 resource per matching region)
  - [ ] Test booster buildings (Iron Foundry, Grain Mill, etc.)
  - [ ] Verify 3-per-region cap enforcement
  - [ ] Test Marketplace bonus timing and logic
  - [ ] Verify production with multiple regions of same type

- [ ] **Test Event Die Faces (1-4)**
  - [ ] Brigand Attack with Storehouse protection
  - [ ] Brigand Attack without Storehouse (>7 Gold+Wool)
  - [ ] Trade event with/without trade advantage
  - [ ] Celebration with skill point tie
  - [ ] Celebration with skill point winner
  - [ ] Plentiful Harvest with Toll Bridge bonus

- [ ] **Test Event Cards (5-6)**
  - [ ] Feud: select buildings, opponent chooses removal
  - [ ] Fraternal Feuds: select hand cards, skip replenish
  - [ ] Invention: gain resources based on progress points
  - [ ] Trade Ships Race: winner/tie scenarios
  - [ ] Traveling Merchant: trade Gold for resources
  - [ ] Year of Plenty: Storehouse/Abbey adjacency bonus
  - [ ] Yule: shuffle and redraw

### Action Card Tests
- [ ] **Test Core Action Cards**
  - [ ] Merchant Caravan: discard 2, gain 2
  - [ ] Goldsmith: discard 3 Gold, gain 2 resources
  - [ ] Scout: choose regions for next settlement
  - [ ] Brigitta: choose production die before roll
  - [ ] Relocation: swap regions or expansions
  - [ ] Road Building: build road from hand
  - [ ] Merchant: trade mechanics

### Placement Tests
- [ ] **Test Center Cards**
  - [ ] Road placement next to other center cards
  - [ ] Settlement placement next to Road
  - [ ] Settlement triggers region placement (2 diagonal)
  - [ ] City upgrade on existing Settlement
  - [ ] Edge expansion when building at column 0 or last

- [ ] **Test Expansion Placement**
  - [ ] Buildings must be above/below Settlement/City
  - [ ] Inner ring fills before outer ring
  - [ ] One-of restrictions (e.g., only 1 Abbey)

### Trade Tests
- [ ] **Test All Trade Types**
  - [ ] TRADE3: 3:1 bank trades
  - [ ] TRADE2: 2:1 with specific trade ships
  - [ ] Large Trade Ship: adjacent 2:1 trades (left and right)
  - [ ] Verify storage capacity checks
  - [ ] Verify resource consumption

### Replenish & Exchange Tests
- [ ] **Test Hand Management**
  - [ ] Replenish to 3 + progress points
  - [ ] Stack choice with empty stack fallback
  - [ ] Fraternal Feuds skip flag
  - [ ] Exchange: random draw vs. search
  - [ ] Parish Hall: 1 resource discount on search

### Win Condition Tests
- [ ] **Test Victory**
  - [ ] Base VP from Settlements (1 VP) and Cities (2 VP)
  - [ ] Trade advantage token (+1 VP)
  - [ ] Strength advantage token (+1 VP)
  - [ ] Win at 7 VP total

---

## ⚠️ Phase 2: Specific Scenario Validation (Priority: High)

### Marketplace Scenarios
```
Scenario 1: Opponent has more regions
- Player A: 2 regions with die face 4
- Player B: 3 regions with die face 4
- Roll production die: 4
- Expected: Player A gets Marketplace bonus
- Verify: Player A gains +1 resource that Player B can produce
```

- [ ] Test Marketplace with varying region counts
- [ ] Test Marketplace when opponent has zero regions on face
- [ ] Test Marketplace with storage capacity limits

### Storehouse Scenarios
```
Scenario 1: Brigand with Storehouse
- Player has Storehouse at (row 1, col 2)
- Player has regions at (row 0, col 1) and (row 0, col 3)
- Gold+Wool total > 7
- Expected: Protected regions keep resources
- Verify: Correct regions are excluded
```

- [ ] Test Storehouse protection on upper side
- [ ] Test Storehouse protection on lower side
- [ ] Test multiple Storehouses
- [ ] Test Year of Plenty with Storehouse adjacency

### Large Trade Ship Scenarios
```
Scenario 1: LTS trade on left side
- LTS at (row 1, col 3)
- Left region (row 1, col 2) has 3 Lumber stored
- Right region (row 1, col 4) has 1 Brick stored
- Player executes: LTS L Lumber Brick
- Expected: -2 Lumber from left, +1 Brick to player
```

- [ ] Test LTS left side trade
- [ ] Test LTS right side trade
- [ ] Test with insufficient resources (should fail)
- [ ] Test with storage capacity at cap

### Scout Scenarios
```
Scenario 1: Scout enables region selection
- Player plays Scout
- Player plays Settlement
- Region stack shows: Forest, Hill, Field
- Player chooses: Hill (index 1), Forest (index 0)
- Expected: Regions placed in chosen order
```

- [ ] Test Scout with region name selection
- [ ] Test Scout with region index selection
- [ ] Test Scout with invalid selection (fallback)

---

## 📊 Phase 3: Side-by-Side Comparison (Priority: Medium)

### Automated Comparison Test
Create a test that runs both versions with:
- [ ] Same random seed
- [ ] Same player input sequence
- [ ] Compare board state after each turn
- [ ] Compare resource counts
- [ ] Compare hand contents
- [ ] Compare VP and advantage tokens

### Manual Playthrough
- [ ] Play full game in legacy version
- [ ] Replay same moves in refactored version
- [ ] Document any differences
- [ ] Update refactored code if discrepancies found

---

## 🧪 Phase 4: Expand Unit Test Coverage (Priority: Medium)

### Model Tests
- [ ] Test RegionTile storage (gain, remove, set)
- [ ] Test Principality.produce() with various die rolls
- [ ] Test Principality booster building detection
- [ ] Test Principality trade ratio calculations
- [ ] Test Player resource methods
- [ ] Test Card hierarchy equality and compareTo

### Controller Tests
- [ ] Test DeckManager.replenishHand() edge cases
- [ ] Test DeckManager.optionalExchange() with Parish Hall
- [ ] Test RuleValidator cost checking
- [ ] Test RuleValidator precondition checking
- [ ] Test EventManager event resolution
- [ ] Test TurnManager turn flow
- [ ] Test AdvantageManager advantage updates

### Effect Tests
- [ ] Test each ICardEffect implementation independently
- [ ] Mock GameContext for isolated testing
- [ ] Verify resource changes
- [ ] Verify board state changes
- [ ] Verify output messages

### Placement Tests
- [ ] Test each PlacementHandler independently
- [ ] Test PlacementRegistry routing
- [ ] Test coordinate validation
- [ ] Test adjacency checks
- [ ] Test one-of restrictions

---

## 📝 Phase 5: Documentation (Priority: Low)

### Code Documentation
- [ ] Add Javadoc to all public APIs
- [ ] Document GameContext usage patterns
- [ ] Document effect registration process
- [ ] Document placement handler contract
- [ ] Document policy interface usage

### Developer Guides
- [ ] Write "Adding a New Action Card" guide
- [ ] Write "Adding a New Event Card" guide
- [ ] Write "Adding a New Building" guide
- [ ] Write "Adding a New Placement Type" guide
- [ ] Write "Testing Your Changes" guide

### Architecture Documentation
- [ ] Update ARCHITECTURE.md with any changes
- [ ] Document design decisions
- [ ] Document trade-offs made
- [ ] Add sequence diagrams for complex flows

---

## 🚀 Phase 6: Performance & Polish (Priority: Low)

### Performance Optimization
- [ ] Profile production phase (hot path)
- [ ] Consider caching RegionTile lookups
- [ ] Consider HashMap for building name lookups
- [ ] Optimize repeated board scans if needed

### Code Quality
- [ ] Run static analysis tools (SpotBugs, PMD)
- [ ] Check code coverage (aim for >80%)
- [ ] Review and refactor any remaining code smells
- [ ] Ensure consistent naming conventions

### User Experience
- [ ] Review all output messages for clarity
- [ ] Ensure error messages are helpful
- [ ] Consider adding color/formatting to console output
- [ ] Add game state summary command

---

## 🔒 Phase 7: Security & Robustness (Priority: Low)

### Input Validation
- [ ] Validate all user inputs (coordinates, card names, etc.)
- [ ] Handle invalid inputs gracefully
- [ ] Prevent index out of bounds errors
- [ ] Handle empty deck scenarios

### Network Security
- [ ] Review socket communication security
- [ ] Add timeout handling for network operations
- [ ] Handle disconnections gracefully
- [ ] Consider adding authentication for online play

---

## 📦 Phase 8: Packaging & Deployment (Priority: Future)

### Build System
- [ ] Create proper Maven/Gradle build file
- [ ] Set up CI/CD pipeline
- [ ] Add pre-commit hooks (formatting, tests)
- [ ] Generate JAR with dependencies

### Deployment
- [ ] Create runnable JAR
- [ ] Write deployment instructions
- [ ] Test on different platforms (Windows, Mac, Linux)
- [ ] Consider Docker containerization

---

## 🎯 Success Criteria

The refactored code is ready for production when:

✅ **Functional Parity**
- All integration tests pass
- No discrepancies in side-by-side comparison
- All medium-risk areas verified

✅ **Quality Standards**
- Unit test coverage > 80%
- All SOLID principles maintained
- Booch metrics within acceptable ranges
- No critical bugs or security issues

✅ **Documentation**
- All public APIs documented
- Developer guides available
- Architecture documented

✅ **Maintainability**
- Code is easy to understand
- Changes are localized
- New features can be added via extension (not modification)

---

## 📞 Getting Help

If you encounter issues or need clarification:

1. **Check documentation**
   - CODE_REVIEW_SUMMARY.md (detailed analysis)
   - COMPARISON_QUICK_REFERENCE.md (quick reference)
   - ARCHITECTURE.md (design decisions)

2. **Review test cases**
   - src/tests/ (existing unit tests)
   - Look for similar scenarios

3. **Consult legacy code**
   - Server.java (reference implementation)
   - Card.java (card effects)
   - Compare line-by-line if needed

4. **Ask for review**
   - Open GitHub issue with specific question
   - Include test cases and expected behavior
   - Reference relevant documentation

---

## 📅 Estimated Timeline

- **Phase 1 (Verification):** 2-3 days
- **Phase 2 (Scenarios):** 1-2 days
- **Phase 3 (Comparison):** 1 day
- **Phase 4 (Unit Tests):** 2-3 days
- **Phase 5 (Documentation):** 1-2 days
- **Phase 6 (Polish):** 1 day
- **Phase 7 (Security):** 1 day
- **Phase 8 (Deployment):** 1 day

**Total:** ~2 weeks for complete validation and production readiness

---

## 🎉 Current Status

Based on the review:

✅ **Completed:**
- Architecture designed and implemented
- All core features implemented
- 8 unit tests created
- Code compiles successfully
- SOLID principles followed
- Good Booch metrics

⏳ **In Progress:**
- Integration testing (Phase 1)
- Scenario validation (Phase 2)

📋 **Not Started:**
- Side-by-side comparison (Phase 3)
- Full unit test coverage (Phase 4)
- Complete documentation (Phase 5)
- Performance optimization (Phase 6)
- Security review (Phase 7)
- Deployment setup (Phase 8)

**Current Assessment:** ~70% complete, ready for testing phase.

---

**Last Updated:** October 21, 2025  
**Next Review:** After Phase 1-2 completion
