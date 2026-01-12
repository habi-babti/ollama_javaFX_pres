# Ollama JavaFX Chat Application

A modern, feature-rich JavaFX desktop application for interacting with Ollama AI models. This application provides a clean, intuitive interface for AI conversations with advanced theme management, user authentication, and comprehensive settings.

## 🚀 Features

### Core Functionality
- **AI Chat Interface**: Interactive chat with Ollama AI models
- **Model Selection**: Choose from available Ollama models
- **Real-time Streaming**: Live response streaming from AI models
- **Conversation Management**: Clear, save, and manage chat history
- **Message Validation**: Input validation and error handling

### User Management
- **Authentication System**: Secure login with username/password
- **Role-based Access**: User and Admin roles with different permissions
- **User Management**: Admin interface for managing users (create, edit, delete)
- **Session Management**: Secure session handling with logout functionality

### Theme System
- **Multiple Themes**: Light, Dark, and Blue themes
- **Consistent Styling**: Themes applied across all windows and dialogs
- **Font Customization**: Configurable font family and size
- **Live Preview**: Real-time theme preview in settings
- **Persistent Settings**: Theme preferences saved automatically

### Advanced Features
- **Settings Management**: Configurable Ollama URL, system prompts, and appearance
- **Keyboard Shortcuts**: Efficient navigation with hotkeys
- **Connection Testing**: Built-in Ollama server connection testing
- **Responsive UI**: Adaptive layout with proper window management
- **Error Handling**: Comprehensive error messages and recovery

## 📋 Prerequisites

- **Java 21+**: Required for JavaFX and modern Java features
- **Maven 3.6+**: For building and dependency management
- **Ollama Server**: Running Ollama instance (local or remote)

## 🛠️ Installation

### 1. Clone the Repository
```bash
git clone https://github.com/habi-babti/ollama_javaFX_pres.git
cd ollama_javaFX_pres
```

### 2. Build the Application
```bash
# Using Maven wrapper (recommended)
./mvnw clean compile

# Or with system Maven
mvn clean compile
```

### 3. Run the Application
```bash
# Using Maven wrapper
./mvnw javafx:run

# Or with system Maven
mvn javafx:run
```

## 🔧 Configuration

### Ollama Setup
1. Install and start Ollama server
2. Default URL: `http://localhost:11434`
3. Pull some models: `ollama pull llama2`

### First Run
1. Launch the application
2. Default login credentials:
   - **Username**: `admin`
   - **Password**: `admin`
3. Configure Ollama URL in Settings if different from default
4. Test connection and select a model

## 🎨 Themes

The application includes three built-in themes:

### Light Theme
- Clean, bright interface
- Optimal for daytime use
- High contrast for readability

### Dark Theme
- Modern dark interface
- Reduced eye strain
- Perfect for low-light environments

### Blue Theme
- Professional blue color scheme
- Balanced contrast
- Corporate-friendly appearance

## 📱 User Interface

### Login Screen
- Secure authentication
- Remember credentials option
- Clean, focused design

### Main Chat Interface
- Message bubbles with role-based styling
- Model selection dropdown
- Real-time typing indicators
- Conversation controls (clear, refresh, cancel)

### Settings Dialog
- Ollama server configuration
- Theme and font customization
- System prompt configuration
- Connection testing
- Live preview of changes

### User Management (Admin Only)
- Create, edit, and delete users
- Role assignment (User/Admin)
- Password management
- User activity overview

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Enter` | Send message |
| `Shift + Enter` | New line in message |
| `Ctrl + N` | New conversation |
| `Ctrl + R` | Refresh models |
| `Escape` | Cancel generation |

## 🏗️ Architecture

### Project Structure
```
src/main/java/com/ollama/olama/
├── controller/          # JavaFX controllers
├── manager/            # Business logic managers
├── model/              # Data models and DTOs
├── service/            # External service integrations
├── ui/                 # Custom UI components
└── util/               # Utility classes

src/main/resources/com/ollama/olama/
├── themes/             # Theme CSS and JSON files
├── *.fxml             # JavaFX FXML layouts
└── styles.css         # Base application styles
```

### Key Components

#### Controllers
- **ChatController**: Main chat interface logic
- **LoginController**: Authentication handling
- **SettingsController**: Configuration management
- **UserManagementController**: User administration

#### Services
- **OllamaService**: AI model communication
- **AuthenticationService**: User authentication
- **SettingsManager**: Configuration persistence
- **ConversationManager**: Chat history management

#### Models
- **ChatMessage**: Message data structure
- **User**: User account information
- **AppSettings**: Application configuration
- **OllamaModel**: AI model metadata

## 🧪 Testing

The project includes comprehensive tests:

```bash
# Run all tests
./mvnw test

# Run specific test categories
./mvnw test -Dtest="*PropertyTest"  # Property-based tests
./mvnw test -Dtest="*ManagerTest"   # Manager tests
```

### Test Coverage
- Unit tests for core business logic
- Property-based testing with jqwik
- Integration tests for services
- UI component testing

## 🔒 Security

### Authentication
- Secure password handling
- Session-based authentication
- Role-based access control
- Automatic session timeout

### Data Protection
- Local configuration storage
- No sensitive data in logs
- Secure communication with Ollama

## 🚀 Deployment

### Creating Executable JAR
```bash
./mvnw clean package
```

### Native Image (Optional)
For faster startup and smaller footprint:
```bash
./mvnw clean package -Pnative
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Development Guidelines
- Follow Java coding conventions
- Add tests for new features
- Update documentation
- Ensure all tests pass

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Ollama](https://ollama.ai/) - Local AI model runtime
- [JavaFX](https://openjfx.io/) - Modern Java UI toolkit
- [Maven](https://maven.apache.org/) - Build and dependency management

## 📞 Support

For support and questions:
- Create an issue on GitHub
- Check existing documentation
- Review the troubleshooting section

## 🔄 Changelog

### Version 1.0.0
- Initial release
- Complete chat interface
- Theme management system
- User authentication
- Settings management
- User administration

---

**Made with ❤️ using JavaFX and Ollama**