package pl.autoreroll.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.autoreroll.AutoRerollClient;
import pl.autoreroll.RerollConfigScreen;

/** Tryb wyboru slotow: klikniecie slotu w ekwipunku zapisuje jego numer do configu. */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin implements AutoRerollClient.ScreenPos {
	@Shadow
	protected Slot focusedSlot;
	@Shadow
	protected int x;
	@Shadow
	protected int y;

	@Override
	public int autorerollX() {
		return this.x;
	}

	@Override
	public int autorerollY() {
		return this.y;
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void autoreroll$learnSlot(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (AutoRerollClient.learn == AutoRerollClient.LearnMode.NONE) return;

		MinecraftClient client = MinecraftClient.getInstance();
		cir.setReturnValue(true);

		Slot slot = this.focusedSlot;
		if (slot == null) return;

		if (!(slot.inventory instanceof PlayerInventory)) {
			AutoRerollClient.notify(client, "§cKliknij slot w SWOIM ekwipunku (dol okna).");
			return;
		}
		if (slot.getStack().isEmpty()) {
			AutoRerollClient.notify(client, "§cTo pusty slot - kliknij slot z przedmiotem.");
			return;
		}

		if (AutoRerollClient.learn == AutoRerollClient.LearnMode.SWORD) {
			AutoRerollClient.CONFIG.swordIndex = slot.getIndex();
			AutoRerollClient.notify(client, "Miecz ustawiony (slot " + slot.getIndex() + ").");
		} else {
			AutoRerollClient.CONFIG.scrollIndex = slot.getIndex();
			AutoRerollClient.notify(client, "Zmianki ustawione (slot " + slot.getIndex() + ").");
		}
		AutoRerollClient.CONFIG.save();
		AutoRerollClient.learn = AutoRerollClient.LearnMode.NONE;
		client.setScreen(new RerollConfigScreen(null));
	}
}
