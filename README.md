# TeaSafeDrop

![Version](https://img.shields.io/badge/version-1.0.0-green)
![API](https://img.shields.io/badge/API-1.21-blue)
![Folia](https://img.shields.io/badge/Folia-Supported-success)

## 📝 Description

TeaSafeDrop is a lightweight yet powerful Minecraft plugin designed to protect valuable items from being destroyed by explosions or fire. Perfect for survival servers, minigame arenas, or any environment where preserving important items is crucial, this plugin ensures that your configured items remain safe even in dangerous situations.

## ✨ Features

- **Item Protection**: Safeguard specific items from explosions, fire, and lava damage
- **Per-World Configuration**: Set up different protection rules for each world
- **Customizable Item List**: Choose exactly which items deserve protection
- **Default Settings**: Set global defaults that apply to unconfigured worlds
- **Debug Mode**: Detailed logging for troubleshooting
- **Folia Support**: Full compatibility with the Folia server implementation

## 🔧 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ts` or `/teasafedrop` | Display plugin information | `teasafedrop.use` |
| `/ts reload` | Reload the plugin configuration | `teasafedrop.reload` |
| `/ts help` | Show command help | `teasafedrop.use` |

## 🔒 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `teasafedrop.use` | Access to base commands | OP      |
| `teasafedrop.reload` | Reload configuration | OP      |
| `teasafedrop.*` | All permissions | OP      |

## 🛠️ Installation

1. Download the TeaSafeDrop plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Edit the configuration in the generated config.yml file

## ⚙️ Configuration

TeaSafeDrop offers extensive configuration options through its config.yml file:

```yaml
# Global settings
settings:
  debug: false
  default-protection-enabled: true
  
# Items to protect from explosions and fire
protected-items:
  DIAMOND: true
  DIAMOND_SWORD: true
  NETHERITE_INGOT: true
  # Add more items as needed
  
# Per-world configurations
worlds:
  world:
    protection-enabled: true
    # World-specific item overrides
    protected-items:
      # Override global settings for this world
      # DIAMOND: false
```

## 💡 Usage Examples

**Add a new item to protect:**
1. Open config.yml
2. Add the item under the `protected-items` section:
```yaml
protected-items:
  ENCHANTED_GOLDEN_APPLE: true
```
3. Reload the plugin with `/ts reload`

**Disable protection in a specific world:**
```yaml
worlds:
  minigame_world:
    protection-enabled: false
```

**Override global settings for a specific world:**
```yaml
worlds:
  resource_world:
    protection-enabled: true
    protected-items:
      DIAMOND: false  # Don't protect diamonds in this world
      EMERALD: true   # Protect emeralds in this world
```

## 📋 Support

For issues, feature requests, or assistance, please open an issue on our GitHub repository or contact the author.

---

Developed with ❤️ by Nighter