package com.dotcms.rest.api.v1.sites.ruleengine;

import com.dotcms.repackage.com.fasterxml.jackson.annotation.JsonProperty;
import com.dotcms.repackage.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.dotcms.repackage.javax.validation.constraints.NotNull;
import com.dotcms.rest.api.Validated;
import com.dotcms.rest.exception.BadRequestException;

import static com.dotcms.rest.validation.Preconditions.checkNotNull;

@JsonDeserialize(builder = RestRuleActionParameter.Builder.class)
public class RestRuleActionParameter extends Validated {

    public final String id;

    @NotNull
    public final String key;

    @NotNull
    public final String value;

    private RestRuleActionParameter(Builder builder) {
        id = builder.id;
        key = builder.key;
        value = builder.value;
        checkValid();
    }

    public static final class Builder {
        @JsonProperty private String id;
        @JsonProperty private String key;
        @JsonProperty private String value;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public RestRuleActionParameter build() {
            return new RestRuleActionParameter(this);
        }
    }
}
