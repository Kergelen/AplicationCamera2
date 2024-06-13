const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const app = express();
const upload = multer({ dest: 'uploads/' });

app.post('/upload', upload.single('image'), (req, res) => {
    // Check if file was uploaded
    if (!req.file) {
        return res.status(400).send('No file uploaded');
    }

    // Rename the file to have the correct extension
    const newFilename = req.file.path + path.extname(req.file.originalname);
    fs.renameSync(req.file.path, newFilename);

    // Send a response
    res.send('File uploaded successfully');
});

app.get('/image', (req, res) => {
    // Serve the latest uploaded image
    fs.readdir('uploads/', (err, files) => {
        if (err) {
            res.status(500).send('An error occurred');
            return;
        }

        const latestFile = files.sort((a, b) => {
            return fs.statSync('uploads/' + b).mtime.getTime() - 
                   fs.statSync('uploads/' + a).mtime.getTime();
        })[0];

        res.sendFile(path.resolve('uploads/' + latestFile));
    });
});

app.listen(3000, () => {
    console.log('Server is running on port 3000');
});