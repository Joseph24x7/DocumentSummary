import { useState } from 'react';
import DocumentUpload from './components/DocumentUpload';
import ChatBot from './components/ChatBot';
import './App.css';

function App() {
  const [chatSession, setChatSession] = useState(null);

  const handleUploadComplete = (uploadResponse) => {
    // Store session data when upload is complete
    if (uploadResponse && uploadResponse.sessionId) {
      setChatSession({
        sessionId: uploadResponse.sessionId,
        documentId: uploadResponse.documentId,
        documentName: uploadResponse.documentName,
        initialResponse: uploadResponse.response,
      });
    }
  };

  const handleUploadNew = () => {
    // Clear session to return to upload screen
    setChatSession(null);
  };

  return (
    <div className="app">
      {chatSession ? (
        <ChatBot
          sessionId={chatSession.sessionId}
          documentName={chatSession.documentName}
          onUploadNew={handleUploadNew}
        />
      ) : (
        <DocumentUpload onUploadComplete={handleUploadComplete} />
      )}
    </div>
  );
}

export default App;

