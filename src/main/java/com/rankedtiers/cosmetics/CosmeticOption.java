package com.rankedtiers.cosmetics;

/**
 * One selectable armor-trim building block (either a pattern or a material),
 * gated behind a minimum ranked tier from config.yml.
 */
public record CosmeticOption(String id, String requiredTier) {
}
