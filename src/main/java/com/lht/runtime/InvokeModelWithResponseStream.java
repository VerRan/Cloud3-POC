package com.lht.runtime;// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import org.json.JSONArray;
// snippet-start:[bedrock-runtime.java2.invoke_model_with_response_stream.import]
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
// snippet-end:[bedrock-runtime.java2.invoke_model_with_response_stream.import]

/**
 * Before running this Java V2 code example, set up your development
 * environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
public class InvokeModelWithResponseStream {

        // snippet-start:[bedrock-runtime.java2.invoke_model_with_response_stream.main]
        /**
         * Invokes the Anthropic Claude 2 model and processes the response stream.
         *
         * @param prompt The prompt for Claude to complete.
         * @param silent Suppress console output of the individual response stream
         *               chunks.
         * @return The generated response.
         */
        public static String invokeClaude(String prompt, boolean silent) {

                BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                                .region(Region.US_WEST_2)
                                .credentialsProvider(ProfileCredentialsProvider.create())
                                .build();

                var finalCompletion = new AtomicReference<>("");

                var payload = new JSONObject()
                                .put("prompt", "Human: " + prompt + " Assistant:")
                                .put("temperature", 0.8)
                                .put("max_tokens_to_sample", 300)
                                .toString();

                var request = InvokeModelWithResponseStreamRequest.builder()
                                .body(SdkBytes.fromUtf8String(payload))
                                .modelId("anthropic.claude-v2")
                                .contentType("application/json")
                                .accept("application/json")
                                .build();

                var visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                                .onChunk(chunk -> {
                                        var json = new JSONObject(chunk.bytes().asUtf8String());
                                        var completion = json.getString("completion");
                                        finalCompletion.set(finalCompletion.get() + completion);
                                        if (!silent) {
                                                System.out.print(completion);
                                        }
                                })
                                .build();

                var handler = InvokeModelWithResponseStreamResponseHandler.builder()
                                .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor)))
                                .onComplete(() -> {
                                })
                                .onError(e -> System.out.println("\n\nError: " + e.getMessage()))
                                .build();

                client.invokeModelWithResponseStream(request, handler).join();

                return finalCompletion.get();
        }
        // snippet-end:[bedrock-runtime.java2.invoke_model_with_response_stream.main]

        // snippet-start:[bedrock-runtime.java2.invoke_model_with_response_stream.main]
        /**
         * Invokes the Anthropic Claude 2 model and processes the response stream.
         *
         * @param prompt The prompt for Claude to complete.
         * @param silent Suppress console output of the individual response stream
         *               chunks.
         * @return The generated response.
         */
        public static String invokeClaude3(String modelId, String prompt, boolean silent) {

                BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                                .region(Region.US_WEST_2)
                                .credentialsProvider(ProfileCredentialsProvider.create())
                                .build();

                var finalCompletion = new AtomicReference<>("");

                JSONObject content = new JSONObject();
                content.put("type", "text");
                content.put("text", prompt);

                JSONArray contentArray = new JSONArray();
                contentArray.put(content);

                JSONObject messages = new JSONObject();
                messages.put("role", "user");
                messages.put("content", contentArray);

                JSONArray arrayElementOneArray = new JSONArray();
                arrayElementOneArray.put(messages);
                var payload = new JSONObject()
                                .put("anthropic_version", "bedrock-2023-05-31")
                                .put("max_tokens", 200)
                                .put("system", "You are an AI bot")
                                .put("messages", arrayElementOneArray)
                                .toString();

                var request = InvokeModelWithResponseStreamRequest.builder()
                                .body(SdkBytes.fromUtf8String(payload))
                                .modelId(modelId)
                                .contentType("application/json")
                                .accept("application/json")
                                .build();

                var visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                                .onChunk(chunk -> {
                                        var json = new JSONObject(chunk.bytes().asUtf8String());
                                        //System.out.print(json.getString("type"));
                                        if (json.getString("type").equals("content_block_delta")){
                                                //var completion = json.getJSONArray("delta").toString();
                                                var completion = json.getJSONObject("delta").getString("text");
                                                finalCompletion.set(finalCompletion.get() + completion);
                                                if (!silent) {
//                                                        System.out.print("hi");
                                                        System.out.print(completion);
                                                }
                                        }
                                        
                                })
                                .build();

                var handler = InvokeModelWithResponseStreamResponseHandler.builder()
                                .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor)))
                                .onComplete(() -> {
                                })
                                .onError(e -> System.out.println("\n\nError: " + e.getMessage()))
                                .build();

                client.invokeModelWithResponseStream(request, handler).join();

                return finalCompletion.get();
        }
        // snippet-end:[bedrock-runtime.java2.invoke_model_with_response_stream.main]
}
