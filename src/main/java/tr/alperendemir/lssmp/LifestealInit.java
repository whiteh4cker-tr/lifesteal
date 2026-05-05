package tr.alperendemir.lssmp;

import tr.alperendemir.helix.BukkitHelixProvider;

public class LifestealInit extends BukkitHelixProvider {

    private Lifesteal plugin;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        this.plugin = new Lifesteal(this);
        this.plugin.onEnable(this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        if (this.plugin != null) {
            this.plugin.reloadConfig(this);
        }
    }

    @Override
    public void onDisable() {
        if (this.plugin != null) {
            this.plugin.onDisable(this);
        }

        super.onDisable();
    }

    @Override
    protected boolean shouldLogBootMessages() {
        return false;
    }
}
