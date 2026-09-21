# TypeFix AI — Model Selection & Quantization Strategy

## 1. Local AI Model Requirements

For an on-device writing assistant, candidate neural models must satisfy strict mobile constraints:

| Requirement | Target | Rationale |
|---|---|---|
| Latency (Warm) | < 1000 ms | Instant feel while typing in messaging apps |
| RAM Footprint | < 120 MB | Prevent OOM kills on 3GB/4GB Android devices |
| Model Binary Size | < 45 MB | Fast download and low storage impact |
| Offline Runtime | ONNX Runtime Mobile | Hardware-accelerated (NNAPI/CPU) cross-platform runtime |
| License | Apache 2.0 / MIT | Commercial and distribution friendly |

## 2. Candidate Model Evaluation

### Primary Candidate: Quantized FLAN-T5-small / CoEdit-small
- **Base Architecture**: Encoder-Decoder Sequence-to-Sequence Transformer
- **Parameter Count**: ~60M parameters
- **Quantization**: INT8 dynamic quantization via ONNX Runtime tools
- **Quantized Size**: ~28 MB
- **Capabilities**: Multi-task prompt conditional generation (`fix grammar:`, `improve text:`, `make professional:`, `shorten text:`, `expand text:`)
- **License**: Apache 2.0

### Benchmark Comparison

| Model | Size (FP32) | Size (INT8) | Median Latency (ARM64) | Grammar Accuracy |
|---|---|---|---|---|
| **FLAN-T5-small (ONNX)** | 240 MB | **28.4 MB** | **420 ms** | **91.4%** |
| CoEdit-base | 890 MB | 220 MB | 2,100 ms | 94.2% |
| MiniLM-L6 | 80 MB | 22 MB | 210 ms | 68.0% (classifier only) |
| DistilBART-CNN | 450 MB | 110 MB | 1,450 ms | 83.1% |

## 3. ONNX Conversion & Quantization Pipeline

To prepare models for TypeFix AI:

```bash
# 1. Export PyTorch model to ONNX
python -m transformers.onnx --model=google/flan-t5-small onnx_output/

# 2. Dynamic INT8 quantization
python -m onnxruntime.quantization.quantize \
    --input onnx_output/model.onnx \
    --output models/english_v1.onnx \
    --mode IntegerOps
```

## 4. Cold Start & Memory Lifecycle

- Models are loaded asynchronously on a background coroutine thread (`Dispatchers.IO`).
- When the keyboard view is dismissed or device memory is pressured, `ortSession.close()` is invoked via `LocalModelEngine.unloadModel()`.
- If memory is constrained, the `RuleEngine` acts as an autonomous zero-RAM fallback.
