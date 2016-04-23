package pl.baczkowicz.spy.ui.stats;


public interface StatsIO
{
	SpyStats loadStats();

	boolean saveStats(final SpyStats stats);
}