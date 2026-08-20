const express = require('express');
const fs = require('fs');
const { port, uploadDir } = require('./config/env');
const documentRoutes = require('./routes/document.routes');
const errorHandler = require('./middleware/error.middleware');

fs.mkdirSync(uploadDir, { recursive: true });

const app = express();
app.use(express.json());

app.use('/api/v1/kyc/documents', documentRoutes);

app.use((req, res) => res.status(404).json({ error: 'Route not found' }));
app.use(errorHandler);

app.listen(port, () => console.log(`pan-card-kyc-service listening on port ${port}`));

module.exports = app;
