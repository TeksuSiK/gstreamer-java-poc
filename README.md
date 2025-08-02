# GStreamer Java - PoC

This PoC provides a simple REST API to manage:
- **Ingresses** (stream inputs)
- **Egresses** (stream outputs)
- Mappings of Egresses assigned to Ingresses
  and to control the forwarding of these streams using GStreamer.

Streams can be started and stopped on demand based on configured mappings.
State and configuration are kept in-memory for simplicity.

---

## API Endpoints

### Ingress

- **Create Ingress**  
  `POST /api/ingress`  
  **Accepts:** JSON with `{ "name": string, "url": string }`  
  **Returns:** 200 OK `"Ingress created"` or 400 if name exists

- **Delete Ingress**  
  `DELETE /api/ingress/{name}`  
  **Returns:** 200 OK `"Ingress deleted"`

- **List all Ingresses**  
  `GET /api/ingress`  
  **Returns:** JSON array of all ingress objects

---

### Egress

- **Create Egress**  
  `POST /api/egress`  
  **Accepts:** JSON with `{ "name": string, "url": string }`  
  **Returns:** 200 OK `"Egress created"` or 400 if name exists

- **Delete Egress**  
  `DELETE /api/egress/{name}`  
  **Returns:** 200 OK `"Egress deleted"`

- **List all Egresses**  
  `GET /api/egress`  
  **Returns:** JSON array of all egress objects

---

### Mapping (Egress ⇄ Ingress)

- **Assign existing Egress to Ingress**  
  `POST /api/mapping/{ingressName}/add/{egressName}`  
  **Returns:** 200 OK `"Egress added to ingress"` or 400 if egress is already assigned elsewhere

- **Remove Egress from Ingress**  
  `POST /api/mapping/{ingressName}/remove/{egressName}`  
  **Returns:** 200 OK `"Egress removed from ingress"`

- **Get Mapping for Ingress**  
  `GET /api/mapping/{ingressName}`  
  **Returns:** JSON object with ingress and its assigned egresses, or 404 if ingress not found

#### Notes
- Each Egress can only be assigned to one Ingress. Trying to assign the same egress to another ingress returns an error.
- Deleting an Ingress also deletes its mapping.
- Deleting an Egress removes it from any mappings it belonged to.

---

### GST

- **Start Stream**  
    `POST /api/stream/start/{ingressName}`  
    **Returns:** 200 OK `"Stream started for ingress {ingressName}"` or 400 if no mapping found or stream already running

- **Stop Stream**  
    `POST /api/stream/stop/{ingressName}`  
    **Returns:** 200 OK `"Stream stopped for ingress {ingressName}"` or 400 if stream not currently running

- **Check Stream Status**  
    `GET /api/stream/status/{ingressName}`  
    **Returns:** 200 OK with body `"running"` if stream is active or 200 OK with body `"stopped"` if stream is not active

---

## GstStreamSession Pipeline Overview

This pipeline enables passthrough streaming from a single RTMP input (Ingress) to multiple RTMP outputs (Egress) using GStreamer.

---

### Pipeline Structure

- **Input Source**
    - `rtmpsrc` (pull RTMP stream from the Ingress URL)
    - `flvdemux` (demultiplex FLV stream into audio and video)

- **Audio and Video Processing**
    - Video path:
        - `queueVideoIn` → `h264parse` → `teeVideo`
    - Audio path:
        - `queueAudioIn` → `aacparse` → `teeAudio`

- **Branching to multiple outputs – for each Egress:**
    - Video branch:
        - `teeVideo` → dynamic src pad → `queueVideoOut`
    - Audio branch:
        - `teeAudio` → dynamic src pad → `queueAudioOut`
    - Muxing and sending:
        - `queueVideoOut` + `queueAudioOut` → `flvmux` → `rtmpsink` (Egress target URL)

---

### Dynamic Behavior

- **Dynamic pads from `flvdemux`:** Audio and video pads are created and linked at runtime.
- **Dynamic `tee` pads:** For each egress, new pads are requested and linked dynamically, enabling flexible branching.
- **Adding/removing egress branches at runtime:** You can add or remove outputs without restarting the pipeline.

---

### Summary

- A single RTMP input stream is demuxed and parsed.
- Audio and video streams are split to multiple output branches using `tee`.
- Each output branch muxes audio/video and outputs to its own RTMP sink.
- Dynamic pad management allows flexible, runtime changes to outputs.
